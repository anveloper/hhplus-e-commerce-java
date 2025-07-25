package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.coupon.*;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.exception.CouponSoldOutException;
import kr.hhplus.be.server.exception.InvalidCouponException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CouponServiceTest {

    private CouponPolicyRepository couponPolicyRepository;
    private CouponRepository couponRepository;
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        couponPolicyRepository = mock(CouponPolicyRepository.class);
        couponRepository = mock(CouponRepository.class);
        couponService = new CouponService(couponPolicyRepository, couponRepository);
    }

    @Test
    @DisplayName("쿠폰 발급 - 성공")
    void issueCoupon_success() {
        User user = new User("테스트 사용자"); // id 없음
        CouponPolicy policy = CouponPolicy.builder()
                .discountAmount(1000)
                .availableCount(10)
                .remainingCount(10)
                .expireDays(30)
                .startedAt(new Date(System.currentTimeMillis() - 1000))
                .endedAt(new Date(System.currentTimeMillis() + 1000 * 60))
                .build();

        when(couponPolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(couponRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Coupon coupon = couponService.issueCoupon(user, 1L);

        assertThat(coupon).isNotNull();
        assertThat(coupon.getUserId()).isEqualTo(user.getId());
        assertThat(coupon.getPolicy()).isEqualTo(policy);
        assertThat(coupon.isUsed()).isFalse();
    }

    @Test
    @DisplayName("쿠폰 발급 - 존재하지 않는 정책")
    void issueCoupon_invalidPolicy() {
        User user = new User("테스트 사용자");

        when(couponPolicyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.issueCoupon(user, 999L))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("존재하지 않는 쿠폰 정책");
    }

    @Test
    @DisplayName("쿠폰 발급 - 만료된 정책")
    void issueCoupon_expiredPolicy() {
        User user = new User("테스트 사용자");

        CouponPolicy expiredPolicy = CouponPolicy.builder()
                .discountRate(0.1)
                .availableCount(10)
                .remainingCount(10)
                .expireDays(30)
                .startedAt(new Date(System.currentTimeMillis() - 100000))
                .endedAt(new Date(System.currentTimeMillis() - 1000)) // 만료
                .build();

        when(couponPolicyRepository.findById(2L)).thenReturn(Optional.of(expiredPolicy));

        assertThatThrownBy(() -> couponService.issueCoupon(user, 2L))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("유효하지 않습니다");
    }

    @Test
    @DisplayName("쿠폰 발급 - 수량 초과")
    void issueCoupon_soldOut() {
        User user = new User("테스트 사용자");

        CouponPolicy soldOutPolicy = CouponPolicy.builder()
                .discountAmount(1000)
                .discountRate(0.1)
                .availableCount(10)
                .remainingCount(0) // 수량 없음
                .expireDays(30)
                .startedAt(new Date(System.currentTimeMillis() - 1000))
                .endedAt(new Date(System.currentTimeMillis() + 1000 * 60))
                .build();

        when(couponPolicyRepository.findById(3L)).thenReturn(Optional.of(soldOutPolicy));

        assertThatThrownBy(() -> couponService.issueCoupon(user, 3L))
                .isInstanceOf(CouponSoldOutException.class)
                .hasMessageContaining("남은 쿠폰 수량이 없습니다");
    }

}

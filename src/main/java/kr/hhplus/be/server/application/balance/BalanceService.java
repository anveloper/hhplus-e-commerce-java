package kr.hhplus.be.server.application.balance;

import kr.hhplus.be.server.domain.balance.*;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.exception.UserNotFoundException;
import kr.hhplus.be.server.exception.InsufficientBalanceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;

    @Transactional
    public void chargeBalance(User user, int amount) {

        Balance balance = user.getBalance();
        if (balance == null) {
            balance = new Balance(user, 0);
            user.setBalance(balance); // 연관관계 설정
        }

        balance.charge(amount);
        balanceRepository.save(balance);

        BalanceHistory history = new BalanceHistory(
                balance,
                amount,
                balance.getBalance(),
                BalanceChangeType.CHARGE
        );
        balanceHistoryRepository.save(history);
    }

    @Transactional
    public void useBalance(User user, int amount) {

        Balance balance = user.getBalance();
        if (balance == null || balance.getBalance() < amount) {
            throw new InsufficientBalanceException("잔액이 부족합니다.");
        }

        balance.use(amount);
        balanceRepository.save(balance);

        BalanceHistory history = new BalanceHistory(
                balance,
                -amount,
                balance.getBalance(),
                BalanceChangeType.USE
        );
        balanceHistoryRepository.save(history);
    }
}

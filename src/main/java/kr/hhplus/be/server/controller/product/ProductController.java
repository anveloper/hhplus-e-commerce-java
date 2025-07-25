package kr.hhplus.be.server.controller.product;

import kr.hhplus.be.server.application.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductApi {

    private final ProductService productService;

    @Override
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Override
    public ResponseEntity<ProductResponseDto> getProductById(Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }
}

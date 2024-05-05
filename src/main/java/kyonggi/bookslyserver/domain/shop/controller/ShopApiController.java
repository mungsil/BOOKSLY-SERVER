package kyonggi.bookslyserver.domain.shop.controller;



import kyonggi.bookslyserver.domain.shop.dto.request.ShopCreateRequestDto;
import kyonggi.bookslyserver.domain.shop.dto.response.shop.ShopCreateResponseDto;
import kyonggi.bookslyserver.domain.shop.dto.response.shop.ShopOwnerDetailReadOneDto;
import kyonggi.bookslyserver.domain.shop.dto.response.shop.ShopRegisterDto;
import kyonggi.bookslyserver.domain.shop.service.ShopService;
import kyonggi.bookslyserver.global.common.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ShopApiController {

    private final ShopService shopService;

    //가게 상세 프로필 조회(가게 주인)
    @GetMapping("/api/shop/owner/{shopId}")
    public ResponseEntity<SuccessResponse<?>> readShop(@PathVariable("shopId") Long id){
        ShopOwnerDetailReadOneDto result = shopService.readOne(id);
        return SuccessResponse.ok(result);
    }


    //가게 등록
    @PostMapping("/api/shop/{shopOwnerId}")
    public ResponseEntity<SuccessResponse<?>> createShop(@PathVariable("shopOwnerId") Long id, @RequestBody @Validated ShopCreateRequestDto requestDto){
        ShopRegisterDto result = shopService.join(id, requestDto);
        return SuccessResponse.ok(result);
    }

    //가게 수정
    @PutMapping("/api/shop/{shopId}")
    public ResponseEntity<SuccessResponse<?>> updateShop(@PathVariable("shopId") Long id, @RequestBody @Validated ShopCreateRequestDto requestDto){
        ShopCreateResponseDto result = shopService.update(id, requestDto);
        return SuccessResponse.ok(result);
    }

    //가게 삭제
    @DeleteMapping("/api/shop/{shopId}")
    public void deleteShop(@PathVariable("id") Long id){
        shopService.delete(id);
    }

}

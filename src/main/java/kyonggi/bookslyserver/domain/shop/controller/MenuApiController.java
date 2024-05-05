package kyonggi.bookslyserver.domain.shop.controller;



import kyonggi.bookslyserver.domain.shop.dto.request.MenuCategoryCreateDto;
import kyonggi.bookslyserver.domain.shop.dto.request.MenuCreateRequestDto;
import kyonggi.bookslyserver.domain.shop.dto.response.menu.MenuCategoryCreateResponseDto;
import kyonggi.bookslyserver.domain.shop.dto.response.menu.MenuCategoryReadDto;
import kyonggi.bookslyserver.domain.shop.dto.response.menu.MenuCreateResponseDto;
import kyonggi.bookslyserver.domain.shop.dto.response.menu.MenuUpdateResponseDto;
import kyonggi.bookslyserver.domain.shop.service.MenuService;
import kyonggi.bookslyserver.global.common.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MenuApiController {

    private final MenuService menuService;



    @PostMapping("/api/menu/{shopId}")
    public ResponseEntity<SuccessResponse<?>> createMenu(@PathVariable("shopId") Long id, @RequestBody @Validated MenuCreateRequestDto requestDto){
        MenuCreateResponseDto result = menuService.create(id, requestDto);
        return SuccessResponse.ok(result);
    }


    @PutMapping("/api/menu/{menuId}")
    public ResponseEntity<SuccessResponse<?>> updateMenu(@PathVariable("menuId") Long id, @RequestBody @Validated MenuCreateRequestDto requestDto){
        MenuUpdateResponseDto result = menuService.update(id, requestDto);
        return SuccessResponse.ok(result);
    }

    @DeleteMapping("/api/menu/{menuId}")
    public void delete(@PathVariable("menuId") Long id){
        menuService.delete(id);
    }

    @GetMapping("/api/menuCategory/owner/{shopId}")
    public ResponseEntity<SuccessResponse<?>> readCategory(@PathVariable("shopId") Long id){
        List<MenuCategoryReadDto> result = menuService.readMenuCategory(id);
        return SuccessResponse.ok(result);
    }

    @PostMapping("/api/menuCategory/{shopId}")
    public ResponseEntity<SuccessResponse<?>> createMenuCategory(@PathVariable("shopId") Long id, @RequestBody @Validated MenuCategoryCreateDto requestDto){
        MenuCategoryCreateResponseDto result = menuService.createCategory(id, requestDto);
        return SuccessResponse.ok(result);
    }

    @PutMapping("/api/menuCategory/{categoryId}")
    public ResponseEntity<SuccessResponse<?>> updateMenuCategory(@PathVariable("categoryId") Long id, @RequestBody @Validated MenuCategoryCreateDto requestDto){
        MenuCategoryCreateDto result = menuService.updateCategory(id, requestDto);
        return SuccessResponse.ok(result);
    }

    @DeleteMapping("/api/menuCategory/{categoryId}")
    public void deleteMenuCategory(@PathVariable("categoryId") Long id){
        menuService.deleteCategory(id);
    }
}

package kyonggi.bookslyserver.domain.shop.controller;

import kyonggi.bookslyserver.domain.shop.dto.request.EmployeeCreateRequestDto;
import kyonggi.bookslyserver.domain.shop.dto.response.employee.EmployeeCreateResponseDto;
import kyonggi.bookslyserver.domain.shop.service.EmployeeService;
import kyonggi.bookslyserver.global.common.SuccessResponse;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EmployeeApiController {
    final private EmployeeService employeeService;

    @PostMapping("/api/employee/{shopId}")
    public ResponseEntity<SuccessResponse<?>> createEmployee(@PathVariable("shopId") Long id, @RequestBody @Validated EmployeeCreateRequestDto requestDto){
        EmployeeCreateResponseDto result = employeeService.join(id, requestDto);
        return SuccessResponse.ok(result);
    }

    @PutMapping("/api/employee/{employeeId}")
    public ResponseEntity<SuccessResponse<?>> updateEmployee(@PathVariable("employeeId") Long id, @RequestBody @Validated EmployeeCreateRequestDto requestDto){
        Long result = employeeService.update(id, requestDto);
        return SuccessResponse.ok(result);
    }

    @DeleteMapping("/api/employee/{employeeId}")
    public void deleteEmployee(@PathVariable("employeeId") Long id){
        employeeService.delete(id);
    }
}

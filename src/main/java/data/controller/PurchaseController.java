package data.controller;

import data.constants.ErrorCode;
import data.dto.ApiResult;
import data.dto.FileDto;
import data.exception.ValidationException;
import data.service.FileService;
import data.service.PurchaseService;
import data.dto.PurchaseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final FileService fileService;

    @PostMapping("/purchase")
    public ResponseEntity<ApiResult<PurchaseDto.Detail>> createPurchase(
            @RequestHeader("Authorization") String token,
            @Valid PurchaseDto.Request purchaseRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult, ErrorCode.VALIDATION_ERROR);
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResult.created(purchaseService.createPurchase(purchaseRequest, token)));
    }

    @GetMapping("/purchases")
    public ResponseEntity<ApiResult<List<PurchaseDto.Summary>>> findAllPurchase(
            String search_keyword,
            String sort,
            double latitude,
            double longitude,
            int radius,
            @RequestParam(defaultValue = "true") boolean status
    ) {
        Map<String, Object> map = new HashMap<>();
        map.put("search_keyword", search_keyword);
        map.put("sort", sort);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("radius", radius);
        map.put("status", status);

        return ResponseEntity.ok(ApiResult.ok(purchaseService.findAllPurchase(map)));
    }

    @GetMapping("/purchase/{id}")
    public ResponseEntity<ApiResult<PurchaseDto.Detail>> findById(
            @PathVariable int id,
            @RequestHeader("Authorization") String token
    ) {
        return ResponseEntity.ok(ApiResult.ok(purchaseService.findPurchaseById(id, token)));
    }

    @PatchMapping("/purchase/{id}")
    public ResponseEntity<ApiResult<PurchaseDto.Detail>> updatePurchase(
            @RequestHeader("Authorization") String token,
            @PathVariable int id,
            PurchaseDto.Request purchaseRequest
    ) {
        return ResponseEntity.ok(ApiResult.ok(purchaseService.updatePurchase(purchaseRequest, token, id)));
    }

    @DeleteMapping("/purchase/{id}")
    public ResponseEntity<ApiResult<?>> deletePurchase(@PathVariable int id, @RequestHeader("Authorization") String token) {
        purchaseService.deletePurchase(id, token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }

    @GetMapping("/purchase/{purchaseId}/files")
    public ResponseEntity<ApiResult<List<FileDto.Response>>> findAllFileByPurchaseId(@PathVariable int purchaseId) {
        return ResponseEntity.ok(ApiResult.ok(fileService.findAllFileByPurchaseId(purchaseId)));
    }
}

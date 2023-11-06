package data.controller;

import data.dto.ErrorResponse;
import data.dto.LikeDto;
import data.dto.PurchaseDto;
import data.exception.LikeNotFoundException;
import data.exception.PurchaseNotFoundException;
import data.exception.MissingRequestHeaderException;
import data.service.LikeService;
import data.service.PurchaseService;
import data.util.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/purchase")
public class LikeController {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final LikeService likeService;
    private final JwtProvider jwtProvider;
    private final PurchaseService purchaseService;

    @Autowired
    public LikeController(LikeService likeService, JwtProvider jwtProvider, PurchaseService purchaseService) {
        this.likeService = likeService;
        this.jwtProvider = jwtProvider;
        this.purchaseService = purchaseService;
    }

    @PostMapping("/{purchaseId}/like")
    public ResponseEntity<?> createLike(
            @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String token,
            @PathVariable int purchaseId,
            @RequestBody LikeDto.Request request
    ) {
        try {
            validateToken(token);
            int userId = jwtProvider.parseJwt(token);
            Optional<Integer> likeIdOptional = likeService.findLikeIdByUserIdAndPurchaseId(userId, purchaseId);

            if (request.isValue()) {
                if (likeIdOptional.isPresent()) {
                    return ResponseEntity.ok().build();
                } else {
                    LikeDto likeDto = buildLikeDtoFromTokenAndPurchaseId(token, purchaseId);
                    likeService.createLike(likeDto);
                    return ResponseEntity.status(HttpStatus.CREATED).build();
                }
            } else {
                if (likeIdOptional.isPresent()){
                    likeService.deleteLike(likeIdOptional.get());
                    return ResponseEntity.noContent().build();
                } else {
                    return ResponseEntity.notFound().build();
                }
            }

        } catch (Exception e) {
            return handleException(e);
        }
    }

    private void validateToken(String token) throws MissingRequestHeaderException {
        if (token == null || token.isEmpty()) {
            throw new MissingRequestHeaderException("Required request header 'Authorization' is missing");
        }
    }

    private LikeDto buildLikeDtoFromTokenAndPurchaseId(String token, int purchaseId) {
        PurchaseDto.Detail purchase = purchaseService.findPurchaseById(purchaseId);
        int purchaseIdFromDb = purchase.getId();
        int userId = jwtProvider.parseJwt(token);
        LikeDto likeDto = new LikeDto();
        likeDto.setUserId(userId);
        likeDto.setPurchaseId(purchaseIdFromDb);
        return likeDto;
    }

    private ResponseEntity<?> handleException(Exception e) {
        HttpStatus status;
        String errorCode;
        if (e instanceof MissingRequestHeaderException) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = "MISSING_REQUEST_HEADER";
        } else if (e instanceof PurchaseNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            errorCode = "PURCHASE_NOT_FOUND";
        } else if (e instanceof LikeNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            errorCode = "LIKE_NOT_FOUND";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = "INTERNAL_SERVER_ERROR";
        }

        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), status.value(), errorCode);
        return ResponseEntity.status(status).body(errorResponse);
    }
}
package data.controller;

import data.constants.ErrorCode;
import data.dto.LikeDto;
import data.dto.PurchaseDto;
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
    }

    private void validateToken(String token) throws MissingRequestHeaderException {
        if (token == null || token.isEmpty()) {
            throw new MissingRequestHeaderException("Required request header 'Authorization' is missing", ErrorCode.MISSING_REQUEST_HEADER);
        }
    }

    private LikeDto buildLikeDtoFromTokenAndPurchaseId(String token, int purchaseId) {
        PurchaseDto.Detail purchase = purchaseService.findPurchaseById(purchaseId, token);
        int purchaseIdFromDb = purchase.getId();
        int userId = jwtProvider.parseJwt(token);
        LikeDto likeDto = new LikeDto();
        likeDto.setUserId(userId);
        likeDto.setPurchaseId(purchaseIdFromDb);
        return likeDto;
    }
}

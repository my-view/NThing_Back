package data.controller;

import data.dto.CommentDto;
import data.service.CommentService;
import data.util.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;
    private final JwtProvider jwtProvider;

    @Autowired
    public CommentController(CommentService commentService, JwtProvider jwtProvider) {
        this.commentService = commentService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("")
    public ResponseEntity<?> createComment(
            @RequestHeader("Authorization") String token,
            @RequestBody CommentDto commentDto
    ){
        if(commentDto.getContent() == null || commentDto.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Comment content cannot be empty");
        }

        try {
            int userId = jwtProvider.parseJwt(token);
            commentDto.setUserId(userId);
            commentService.createComment(commentDto);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/purchase/{purchase_id}")
    public ResponseEntity<?> findCommentById(
            @PathVariable int purchase_id
    ){
        try {
            List<CommentDto> comments = commentService.findCommentsByPurchaseId(purchase_id);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateComment(
            @RequestHeader("Authorization") String token,
            @PathVariable int id,
            @RequestBody CommentDto commentDto
    ){
        if(commentDto.getContent() == null || commentDto.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Comment content cannot be empty");
        }

        try {
            int userId = jwtProvider.parseJwt(token);
            commentDto.setId(id);
            commentService.updateComment(userId, commentDto);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(
            @RequestHeader("Authorization") String token,
            @PathVariable int id
    ){
        try {
            int userId = jwtProvider.parseJwt(token);
            commentService.deleteComment(userId, id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    private ResponseEntity<?> handleException(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
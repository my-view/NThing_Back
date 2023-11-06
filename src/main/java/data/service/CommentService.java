package data.service;

import data.dto.CommentDto;
import data.exception.CommentNotFoundException;
import data.exception.DeletedCommentException;
import data.exception.UnauthorizedException;
import data.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CommentService {

    private final CommentMapper commentMapper;

    @Autowired
    public CommentService(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    public void createComment(CommentDto.Request commentDto) {
        validateComment(commentDto);
        commentMapper.createComment(commentDto);
    }

    public List<CommentDto.Response> findCommentsByPurchaseId(int purchaseId, int userId) {
        List<CommentDto.Response> allComments = commentMapper.findCommentsByPurchaseId(purchaseId);
        return organizeComments(allComments, userId);
    }

    public void updateComment(int userId, CommentDto.Request commentDto) {
        CommentDto.Comment comment = new CommentDto.Comment();
        comment.setId(commentDto.getId());
        comment.setContent(commentDto.getContent());
        comment.setIsPrivate(commentDto.getIsPrivate());
        comment.setUserId(userId);
        comment.setPurchaseId(commentDto.getPurchaseId());
        comment.setParentId(commentDto.getParentId());

        CommentDto.Comment existingComment = findExistingComment(comment.getId());
        validateUserPermission(userId, existingComment.getUserId());
        validateComment(commentDto);
        validateIsDeleted(existingComment.isDelete());
        commentMapper.updateComment(commentDto);
    }

    public void deleteComment(int loginId, int id) {
        CommentDto.Comment existingComment = findExistingComment(id);
        validateUserPermission(loginId, existingComment.getUserId());
        validateIsDeleted(existingComment.isDelete());
        commentMapper.deleteComment(id);
    }

    private void validateComment(CommentDto.Request commentDto) {
        if (commentDto == null || commentDto.getContent() == null || commentDto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
    }

    private CommentDto.Comment findExistingComment(int commentId) {
        CommentDto.Comment existingComment = commentMapper.findCommentById(commentId);
        if (existingComment == null) {
            throw new CommentNotFoundException("Comment not found");
        }
        return existingComment;
    }

    private void validateUserPermission(int loginId, int userId) {
        if (loginId != userId) {
            throw new UnauthorizedException("Edit permission denied");
        }
    }

    private void validateIsDeleted(boolean isDeleted) {
        if (isDeleted) {
            throw new DeletedCommentException("Cannot modify a deleted comment");
        }
    }

    private List<CommentDto.Response> organizeComments(List<CommentDto.Response> allComments, int userId) {
        Map<Integer, CommentDto.Response> commentMap = new HashMap<>();
        List<CommentDto.Response> topLevelComments = new ArrayList<>();

        for (CommentDto.Response comment : allComments) {
            comment.setIsAuthorized(true);
            commentMap.put(comment.getId(), comment);

            if (comment.getParentId() == 0) {
                topLevelComments.add(comment);
            }

            if (comment.getIsPrivate() && comment.getUserId() != userId) {
                if (comment.getUserId() != userId) {
                    comment.setIsAuthorized(false);
                }
            }
            comment.setReplies(new ArrayList<>());
        }

        for (CommentDto.Response comment : allComments) {
            if (comment.getParentId() != 0) {
                CommentDto.Response parentComment = commentMap.get(comment.getParentId());
                if (parentComment != null) {
                    if (parentComment.getReplies() == null) {
                        parentComment.setReplies(new ArrayList<>());
                    }
                    parentComment.getReplies().add(comment);
                }
            }
        }
        return topLevelComments;
    }
}

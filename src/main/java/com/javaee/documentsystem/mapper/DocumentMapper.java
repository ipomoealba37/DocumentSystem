package com.javaee.documentsystem.mapper;
import com.javaee.documentsystem.entity.Document;
import org.apache.ibatis.annotations.Param;
import java.util.List;
public interface DocumentMapper {

    List<Document> findAll(@Param("userId") Integer userId);
    List<Document> search(@Param("keyword") String keyword, @Param("userId") Integer userId);
    List<Document> findDeleted(@Param("userId") Integer userId);
    List<Document> searchDeleted(@Param("keyword") String keyword, @Param("userId") Integer userId);
    int insert(Document document);
    int update(Document document);
    int softDelete(@Param("id") Integer id, @Param("userId") Integer userId);
    int softDeleteBatch(@Param("ids") List<Integer> ids, @Param("userId") Integer userId);
    int restore(@Param("id") Integer id, @Param("userId") Integer userId);
    int restoreBatch(@Param("ids") List<Integer> ids, @Param("userId") Integer userId);
    int permanentDelete(@Param("id") Integer id, @Param("userId") Integer userId);
    int permanentDeleteBatch(@Param("ids") List<Integer> ids, @Param("userId") Integer userId);
    List<Document> findByType(@Param("type") String type, @Param("userId") Integer userId);
}

package com.javaee.documentsystem.service;

import com.javaee.documentsystem.entity.Document;
import com.javaee.documentsystem.mapper.DocumentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

    @Service
    public class DocumentService {
        @Autowired
        private DocumentMapper documentMapper;

        public List<Document> findAll(Integer userId) {
            return documentMapper.findAll(userId);
        }

        public List<Document> search(String keyword, Integer userId) {
            if (keyword == null || keyword.trim().isEmpty()) {
                return findAll(userId);
            }
            return documentMapper.search(keyword, userId);
        }

        public List<Document> findDeleted(Integer userId) {
            return documentMapper.findDeleted(userId);
        }

        public List<Document> searchDeleted(String keyword, Integer userId) {
            if (keyword == null || keyword.trim().isEmpty()) {
                return findDeleted(userId);
            }
            return documentMapper.searchDeleted(keyword, userId);
        }

        public boolean add(Document document) {
            return documentMapper.insert(document) > 0;
        }

        public boolean update(Document document, Integer userId) {
            return documentMapper.update(document) > 0;
        }

        public boolean delete(Integer id, Integer userId) {
            return documentMapper.softDelete(id, userId) > 0;
        }

        public boolean deleteBatch(List<Integer> ids, Integer userId) {
        if (ids == null || ids.isEmpty()) return false;
        return documentMapper.softDeleteBatch(ids, userId) > 0;
    }

    public boolean restore(Integer id, Integer userId) {
        return documentMapper.restore(id, userId) > 0;
    }

    public boolean restoreBatch(List<Integer> ids, Integer userId) {
        if (ids == null || ids.isEmpty()) return false;
        return documentMapper.restoreBatch(ids, userId) > 0;
    }

    public boolean permanentDelete(Integer id, Integer userId) {
        return documentMapper.permanentDelete(id, userId) > 0;
    }

    public boolean permanentDeleteBatch(List<Integer> ids, Integer userId) {
        if (ids == null || ids.isEmpty()) return false;
        return documentMapper.permanentDeleteBatch(ids, userId) > 0;
    }

    public List<Document> findByType(String type, Integer userId) {
        return documentMapper.findByType(type, userId);
    }
}
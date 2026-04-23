package com.server.recommend.service;

import com.server.article.mapper.CommentMapper;
import com.server.recommend.dto.RecommendDTO;
import com.server.recommend.entity.UserEmbedding;
import com.server.recommend.entity.WorkEmbedding;
import com.server.recommend.mapper.UserEmbeddingMapper;
import com.server.recommend.mapper.WorkEmbeddingMapper;
import com.server.user.entity.UserInfo;
import com.server.user.mapper.UserMapper;
import com.server.work.entity.Work;
import com.server.work.mapper.WorkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private UserEmbeddingMapper userEmbeddingMapper;

    @Autowired
    private WorkEmbeddingMapper workEmbeddingMapper;

    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentMapper commentMapper;

    private static final int MAX_FEATURES = 128;

    @Override
    public List<RecommendDTO> getRecommendations(Long userId, Integer limit) {
        try {
            // 1. 获取或生成用户 embedding（懒加载）
            UserEmbedding userEmbedding = userEmbeddingMapper.findByUserId(userId);
            double[] userVector;
            
            if (userEmbedding == null || userEmbedding.getEmbeddingVector() == null) {
                // 首次请求，生成用户 embedding
                updateUserEmbedding(userId);
                userEmbedding = userEmbeddingMapper.findByUserId(userId);
            }
            
            if (userEmbedding == null || userEmbedding.getEmbeddingVector() == null) {
                return getHotWorks(limit);
            }
            userVector = parseVector(userEmbedding.getEmbeddingVector());

            // 2. 获取所有作品 embedding
            List<WorkEmbedding> workEmbeddings = workEmbeddingMapper.findAll();
            if (workEmbeddings.isEmpty()) {
                return getHotWorks(limit);
            }

            // 3. 计算相似度并排序
            List<WorkEmbedding> sortedWorks = workEmbeddings.stream()
                    .filter(we -> we.getEmbeddingVector() != null && !we.getEmbeddingVector().isEmpty())
                    .sorted((a, b) -> {
                        double simA = cosineSimilarity(userVector, parseVector(a.getEmbeddingVector()));
                        double simB = cosineSimilarity(userVector, parseVector(b.getEmbeddingVector()));
                        return Double.compare(simB, simA);
                    })
                    .limit(limit * 3)
                    .collect(Collectors.toList());

            if (sortedWorks.isEmpty()) {
                return getHotWorks(limit);
            }

            // 4. 获取作品详情
            return buildRecommendDTOList(sortedWorks, userVector, limit);

        } catch (Exception e) {
            e.printStackTrace();
            return getHotWorks(limit);
        }
    }

    private List<RecommendDTO> buildRecommendDTOList(List<WorkEmbedding> embeddings, double[] userVector, int limit) {
        List<RecommendDTO> results = new ArrayList<>();

        for (WorkEmbedding we : embeddings) {
            if (results.size() >= limit) break;

            Work work = workMapper.selectById(we.getWorkId());
            if (work == null || work.getStatus() != 1) continue;

            // 查询用户信息
            UserInfo user = userMapper.selectById(work.getUserId());

            RecommendDTO dto = new RecommendDTO();
            dto.setWorkId(work.getId());
            dto.setTitle(work.getTitle());
            dto.setDescription(work.getDescription());
            dto.setCoverImage(work.getCoverImage());
            dto.setType(work.getType());
            dto.setUserId(work.getUserId());
            dto.setViews(work.getViews());
            dto.setLikes(work.getLikes());
            dto.setComments(commentMapper.countWorkComments(work.getId()));
            dto.setScore(cosineSimilarity(userVector, parseVector(we.getEmbeddingVector())));

            if (user != null) {
                dto.setNickname(user.getUsername());
                dto.setAvatar(user.getAvatar());
            }

            if (work.getTags() != null && !work.getTags().isEmpty()) {
                dto.setTags(parseTags(work.getTags()));
            }

            results.add(dto);
        }

        return results;
    }

    private List<String> parseTags(String tagsJson) {
        try {
            if (tagsJson.startsWith("[")) {
                return Arrays.asList(tagsJson.replaceAll("[\\[\\]\"]", "").split(","));
            }
            return Arrays.asList(tagsJson.split(","));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void rebuildAllUserEmbeddings() {
        userEmbeddingMapper.deleteAll();

        List<UserInfo> users = userMapper.selectAllUsers();
        for (UserInfo user : users) {
            try {
                String behaviorText = buildUserBehaviorText(user.getId());
                double[] vector = generateEmbedding(behaviorText);
                if (vector != null) {
                    UserEmbedding ue = new UserEmbedding();
                    ue.setUserId(user.getId());
                    ue.setEmbeddingVector(vectorToString(vector));
                    ue.setUpdatedAt(java.time.LocalDateTime.now());
                    userEmbeddingMapper.saveOrUpdate(ue);
                }
            } catch (Exception e) {
                System.err.println("生成用户 " + user.getId() + " embedding 失败: " + e.getMessage());
            }
        }
    }

    @Override
    public void rebuildAllWorkEmbeddings() {
        workEmbeddingMapper.deleteAll();

        List<Work> works = workMapper.selectAllPublishedWorks();
        for (Work work : works) {
            try {
                String text = buildWorkText(work);
                double[] vector = generateEmbedding(text);
                if (vector != null) {
                    WorkEmbedding we = new WorkEmbedding();
                    we.setWorkId(work.getId());
                    we.setEmbeddingVector(vectorToString(vector));
                    we.setUpdatedAt(java.time.LocalDateTime.now());
                    workEmbeddingMapper.saveOrUpdate(we);
                }
            } catch (Exception e) {
                System.err.println("生成帖子 " + work.getId() + " embedding 失败: " + e.getMessage());
            }
        }
    }

    @Override
    public void updateWorkEmbedding(Long workId) {
        try {
            Work work = workMapper.selectById(workId);
            if (work == null) return;

            String text = buildWorkText(work);
            double[] vector = generateEmbedding(text);
            if (vector != null) {
                WorkEmbedding we = new WorkEmbedding();
                we.setWorkId(workId);
                we.setEmbeddingVector(vectorToString(vector));
                we.setUpdatedAt(java.time.LocalDateTime.now());
                workEmbeddingMapper.saveOrUpdate(we);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateUserEmbedding(Long userId) {
        try {
            String behaviorText = buildUserBehaviorText(userId);
            double[] vector = generateEmbedding(behaviorText);
            if (vector != null) {
                UserEmbedding ue = new UserEmbedding();
                ue.setUserId(userId);
                ue.setEmbeddingVector(vectorToString(vector));
                ue.setUpdatedAt(java.time.LocalDateTime.now());
                userEmbeddingMapper.saveOrUpdate(ue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refreshStaleWorkEmbeddings(Integer minutes) {
        try {
            List<WorkEmbedding> staleEmbeddings = workEmbeddingMapper.findStaleEmbeddings(minutes);
            if (staleEmbeddings.isEmpty()) {
                System.out.println("[推荐系统] 没有超过 " + minutes + " 分钟的过期作品Embedding");
                return;
            }
            System.out.println("[推荐系统] 发现 " + staleEmbeddings.size() + " 个过期作品需要重新生成Embedding");
            for (WorkEmbedding we : staleEmbeddings) {
                try {
                    Work work = workMapper.selectById(we.getWorkId());
                    if (work == null || work.getStatus() != 1) continue;
                    
                    String text = buildWorkText(work);
                    double[] vector = generateEmbedding(text);
                    if (vector != null) {
                        we.setEmbeddingVector(vectorToString(vector));
                        we.setUpdatedAt(java.time.LocalDateTime.now());
                        workEmbeddingMapper.saveOrUpdate(we);
                        System.out.println("[推荐系统] 重新生成作品ID=" + we.getWorkId() + " 的Embedding成功");
                    }
                } catch (Exception e) {
                    System.err.println("[推荐系统] 重新生成作品ID=" + we.getWorkId() + " 的Embedding失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Work> findWorksWithoutEmbedding() {
        return workEmbeddingMapper.findWorksWithoutEmbedding();
    }

    private String buildWorkText(Work work) {
        StringBuilder sb = new StringBuilder();
        sb.append(work.getTitle() != null ? work.getTitle() : "");
        sb.append(" ");
        sb.append(work.getDescription() != null ? work.getDescription() : "");
        sb.append(" ");
        sb.append(work.getTags() != null ? work.getTags().replaceAll("[\\[\\]\"]", "") : "");
        sb.append(" ");
        sb.append(work.getType() != null ? work.getType() : "");
        return sb.toString();
    }

    private String buildUserBehaviorText(Long userId) {
        List<Work> likedWorks = workMapper.selectLikedWorksByUserId(userId);
        if (likedWorks.isEmpty()) {
            return "用户喜欢汉服文化相关的内容，关注汉服摄影、设计和穿搭";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("用户喜欢：");
        for (Work work : likedWorks) {
            if (work.getTitle() != null) {
                sb.append(work.getTitle()).append("，");
            }
            if (work.getTags() != null) {
                sb.append(work.getTags().replaceAll("[\\[\\]\"]", "")).append("，");
            }
        }
        String result = sb.toString();
        if (result.endsWith("，")) {
            result = result.substring(0, result.length() - 1);
        }
        return result.isEmpty() ? "用户喜欢汉服文化相关的内容" : result;
    }

    private double[] generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        // 1. 中文分词
        List<String> terms = chineseTokenize(text.toLowerCase());
        if (terms.isEmpty()) {
            return null;
        }

        // 2. 计算TF（词频）
        Map<String, Double> tf = new HashMap<>();
        for (String term : terms) {
            tf.put(term, tf.getOrDefault(term, 0.0) + 1.0);
        }
        for (String term : tf.keySet()) {
            tf.put(term, tf.get(term) / terms.size());
        }

        // 3. 获取IDF值
        Map<String, Double> idf = getIdfValues();

        // 4. 计算TF-IDF向量
        double[] vector = new double[MAX_FEATURES];
        List<String> featureTerms = getFeatureTerms();

        for (int i = 0; i < Math.min(MAX_FEATURES, featureTerms.size()); i++) {
            String term = featureTerms.get(i);
            double tfVal = tf.getOrDefault(term, 0.0);
            double idfVal = idf.getOrDefault(term, 4.0);
            vector[i] = tfVal * idfVal;
        }

        // 5. L2归一化
        double norm = 0;
        for (double v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }

        return vector;
    }

    private List<String> chineseTokenize(String text) {
        List<String> terms = new ArrayList<>();

        String[] phrases = {
            "汉服", "摄影", "设计", "穿搭", "古装", "传统", "文化", "礼仪",
            "摄影作品", "汉服摄影", "传统服饰", "文化传承", "汉服社区",
            "作品", "帖子", "分享", "喜欢", "推荐", "热门", "最新"
        };

        for (String phrase : phrases) {
            if (text.contains(phrase)) {
                terms.add(phrase);
            }
        }

        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            String charStr = String.valueOf(chars[i]);
            if (charStr.matches("[\\s\\p{Punct}]")) {
                continue;
            }
            terms.add(charStr);
            if (i < chars.length - 1) {
                String bigram = charStr + String.valueOf(chars[i + 1]);
                if (!bigram.matches("[\\s\\p{Punct}]+")) {
                    terms.add(bigram);
                }
            }
        }

        return terms;
    }

    private Map<String, Double> getIdfValues() {
        Map<String, Double> idf = new HashMap<>();
        idf.put("汉服", 3.5);
        idf.put("摄影", 3.0);
        idf.put("设计", 3.0);
        idf.put("穿搭", 3.2);
        idf.put("古装", 3.5);
        idf.put("传统", 2.5);
        idf.put("文化", 2.0);
        idf.put("礼仪", 3.8);
        idf.put("作品", 1.5);
        idf.put("帖子", 1.8);
        idf.put("分享", 2.0);
        idf.put("喜欢", 1.2);
        idf.put("推荐", 2.5);
        idf.put("热门", 3.0);
        idf.put("最新", 2.8);
        idf.put("的", 0.1);
        idf.put("是", 0.1);
        idf.put("我", 0.2);
        idf.put("你", 0.2);
        idf.put("了", 0.1);
        idf.put("在", 0.2);
        return idf;
    }

    private List<String> getFeatureTerms() {
        List<String> terms = new ArrayList<>();
        terms.addAll(Arrays.asList(
            "汉服", "摄影", "设计", "穿搭", "古装", "传统", "文化", "礼仪",
            "摄影作品", "汉服摄影", "传统服饰", "文化传承", "汉服社区",
            "作品", "帖子", "分享", "喜欢", "推荐", "热门", "最新",
            "服饰", "妆容", "造型", "历史", "传承", "美丽", "优雅"
        ));
        for (char c = 'a'; c <= 'z'; c++) {
            terms.add(String.valueOf(c));
        }
        for (char c = '0'; c <= '9'; c++) {
            terms.add(String.valueOf(c));
        }
        return terms;
    }

    private double cosineSimilarity(double[] vecA, double[] vecB) {
        if (vecA.length != vecB.length) return 0.0;
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vecA.length; i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double[] parseVector(String vectorStr) {
        if (vectorStr == null || vectorStr.isEmpty()) {
            return new double[MAX_FEATURES];
        }
        String[] parts = vectorStr.split(",");
        double[] vector = new double[MAX_FEATURES];
        for (int i = 0; i < Math.min(parts.length, MAX_FEATURES); i++) {
            try {
                vector[i] = Double.parseDouble(parts[i].trim());
            } catch (NumberFormatException e) {
                vector[i] = 0.0;
            }
        }
        return vector;
    }

    private String vectorToString(double[] vector) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("%.6f", vector[i]));
        }
        return sb.toString();
    }

    private List<RecommendDTO> getHotWorks(Integer limit) {
        List<Work> hotWorks = workMapper.selectHotWorks(limit);
        List<RecommendDTO> results = new ArrayList<>();

        for (Work work : hotWorks) {
            UserInfo user = userMapper.selectById(work.getUserId());

            RecommendDTO dto = new RecommendDTO();
            dto.setWorkId(work.getId());
            dto.setTitle(work.getTitle());
            dto.setDescription(work.getDescription());
            dto.setCoverImage(work.getCoverImage());
            dto.setType(work.getType());
            dto.setUserId(work.getUserId());
            dto.setViews(work.getViews());
            dto.setLikes(work.getLikes());
            dto.setComments(commentMapper.countWorkComments(work.getId()));
            dto.setScore(0.0);

            if (user != null) {
                dto.setNickname(user.getUsername());
                dto.setAvatar(user.getAvatar());
            }

            if (work.getTags() != null) {
                dto.setTags(parseTags(work.getTags()));
            }
            results.add(dto);
        }

        return results;
    }
}

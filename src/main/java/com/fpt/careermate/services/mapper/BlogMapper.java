package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.Blog;
import com.fpt.careermate.services.dto.request.BlogCreationRequest;
import com.fpt.careermate.services.dto.request.BlogUpdateRequest;
import com.fpt.careermate.services.dto.response.BlogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface BlogMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tags", source = "tags", qualifiedByName = "listToString")
    Blog toBlog(BlogCreationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tags", source = "tags", qualifiedByName = "listToString")
    void updateBlog(@MappingTarget Blog blog, BlogUpdateRequest request);

    @Mapping(target = "author.id", source = "author.id")
    @Mapping(target = "author.username", source = "author.username")
    @Mapping(target = "author.email", source = "author.email")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "stringToList")
    BlogResponse toBlogResponse(Blog blog);

    @Named("listToString")
    default String listToString(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return String.join(",", tags);
    }

    @Named("stringToList")
    default List<String> stringToList(String tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}

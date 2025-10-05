package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.BlogRating;
import com.fpt.careermate.services.dto.request.BlogRatingRequest;
import com.fpt.careermate.services.dto.response.BlogRatingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BlogRatingMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "blog", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BlogRating toBlogRating(BlogRatingRequest request);

    @Mapping(target = "blogId", source = "blog.id")
    @Mapping(target = "userId", source = "user.id")
    BlogRatingResponse toBlogRatingResponse(BlogRating blogRating);
}

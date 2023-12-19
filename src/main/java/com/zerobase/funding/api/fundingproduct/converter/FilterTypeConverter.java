package com.zerobase.funding.api.fundingproduct.converter;

import static com.zerobase.funding.api.exception.ErrorCode.INVALID_REQUEST;

import com.zerobase.funding.api.fundingproduct.exception.FundingProductException;
import com.zerobase.funding.api.fundingproduct.type.FilterType;
import org.springframework.core.convert.converter.Converter;

public class FilterTypeConverter implements Converter<String, FilterType> {

    @Override
    public FilterType convert(String source) {
        if (source.isEmpty()) {
            throw new FundingProductException(INVALID_REQUEST);
        }
        return FilterType.valueOf(source.toUpperCase());
    }
}

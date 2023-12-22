package com.zerobase.funding.api.common.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

public class ParamTypeConverterFactory implements ConverterFactory<String, Enum> {

    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new ParamTypeConverter<>(targetType);
    }

    @RequiredArgsConstructor
    public static class ParamTypeConverter<T extends Enum> implements Converter<String, T> {

        private final Class<T> enumType;

        @Override
        public T convert(String source) {
            return (T) Enum.valueOf(enumType, source.toUpperCase());
        }
    }
}

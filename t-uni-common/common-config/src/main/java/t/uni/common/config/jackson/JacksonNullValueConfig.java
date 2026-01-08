package t.uni.common.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Jackson Null 值处理配置
 * <p>
 * 将 null 值转换为对应类型的空值：
 * <ul>
 * <li>null 字符串 → ""</li>
 * <li>null 数组/集合 → []</li>
 * <li>null Map → {}</li>
 * <li>null 自定义对象 → {}</li>
 * <li>null 数字 → 0</li>
 * <li>null 布尔 → false</li>
 * <li>null 日期 → 保持 null</li>
 * </ul>
 */
@Configuration
public class JacksonNullValueConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonNullValueCustomizer() {
        return builder -> builder.postConfigurer(objectMapper -> {
            // 注册自定义的序列化器修改器
            objectMapper.setSerializerFactory(
                    objectMapper.getSerializerFactory()
                            .withSerializerModifier(new NullValueBeanSerializerModifier()));
        });
    }

    /**
     * 自定义 BeanSerializerModifier，用于修改 null 值的序列化行为
     */
    public static class NullValueBeanSerializerModifier extends BeanSerializerModifier {

        @Override
        public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                BeanDescription beanDesc,
                List<BeanPropertyWriter> beanProperties) {
            // 遍历所有属性，根据类型设置 null 值序列化器
            for (int i = 0; i < beanProperties.size(); i++) {
                BeanPropertyWriter writer = beanProperties.get(i);
                JavaType type = writer.getType();

                NullValueType nullValueType = determineNullValueType(type);
                if (nullValueType != null) {
                    beanProperties.set(i, new NullValuePropertyWriter(writer, nullValueType));
                }
            }
            return beanProperties;
        }

        /**
         * 根据类型确定 null 值应该转换成什么
         */
        private NullValueType determineNullValueType(JavaType type) {
            // 字符串类型 -> ""
            if (type.isTypeOrSubTypeOf(String.class) ||
                    type.isTypeOrSubTypeOf(CharSequence.class)) {
                return NullValueType.STRING;
            }

            // 数组或集合类型 -> []
            if (type.isArrayType() ||
                    type.isCollectionLikeType() ||
                    type.isTypeOrSubTypeOf(Collection.class)) {
                return NullValueType.ARRAY;
            }

            // Map 类型 -> {}
            if (type.isTypeOrSubTypeOf(Map.class)) {
                return NullValueType.OBJECT;
            }

            // 布尔类型 -> false
            if (type.isTypeOrSubTypeOf(Boolean.class) ||
                    (type.isPrimitive() && type.getRawClass() == boolean.class)) {
                return NullValueType.BOOLEAN;
            }

            // 数字类型 -> 0
            if (isNumberType(type)) {
                return NullValueType.NUMBER;
            }

            // 枚举类型 -> 保持 null
            if (type.isEnumType()) {
                return null;
            }

            // 日期时间类型 -> 保持 null
            if (isDateTimeType(type)) {
                return null;
            }

            // 其他自定义对象类型 -> {}
            if (!type.isFinal() || type.getRawClass().getPackageName().startsWith("t.uni")) {
                return NullValueType.OBJECT;
            }

            // 其他未知类型的对象也转为 {}
            if (type.isJavaLangObject() || !type.isPrimitive()) {
                return NullValueType.OBJECT;
            }

            return null;
        }

        /**
         * 判断是否是数字类型
         */
        private boolean isNumberType(JavaType type) {
            Class<?> rawClass = type.getRawClass();
            // 数字包装类
            if (rawClass == Integer.class ||
                    rawClass == Long.class ||
                    rawClass == Double.class ||
                    rawClass == Float.class ||
                    rawClass == Short.class ||
                    rawClass == Byte.class ||
                    rawClass == Number.class ||
                    type.isTypeOrSubTypeOf(Number.class)) {
                return true;
            }
            // 数字基本类型
            if (type.isPrimitive()) {
                return rawClass == int.class ||
                        rawClass == long.class ||
                        rawClass == double.class ||
                        rawClass == float.class ||
                        rawClass == short.class ||
                        rawClass == byte.class;
            }
            return false;
        }

        /**
         * 判断是否是日期时间类型
         */
        private boolean isDateTimeType(JavaType type) {
            String typeName = type.getRawClass().getName();
            return typeName.startsWith("java.time.") ||
                    typeName.equals("java.util.Date") ||
                    typeName.equals("java.sql.Date") ||
                    typeName.equals("java.sql.Timestamp");
        }
    }

    /**
     * Null 值类型枚举
     */
    public enum NullValueType {
        STRING, // null -> ""
        ARRAY, // null -> []
        OBJECT, // null -> {}
        NUMBER, // null -> 0
        BOOLEAN // null -> false
    }

    /**
     * 自定义属性序列化器，用于处理 null 值
     */
    public static class NullValuePropertyWriter extends BeanPropertyWriter {

        private final BeanPropertyWriter delegate;
        private final NullValueType nullValueType;

        public NullValuePropertyWriter(BeanPropertyWriter base, NullValueType nullValueType) {
            super(base);
            this.delegate = base;
            this.nullValueType = nullValueType;
        }

        @Override
        public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
            Object value = delegate.get(bean);

            if (value == null) {
                gen.writeFieldName(delegate.getName());
                switch (nullValueType) {
                    case STRING:
                        gen.writeString("");
                        break;
                    case ARRAY:
                        gen.writeStartArray();
                        gen.writeEndArray();
                        break;
                    case OBJECT:
                        gen.writeStartObject();
                        gen.writeEndObject();
                        break;
                    case NUMBER:
                        gen.writeNumber(0);
                        break;
                    case BOOLEAN:
                        gen.writeBoolean(false);
                        break;
                }
            } else {
                delegate.serializeAsField(bean, gen, prov);
            }
        }
    }
}

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
 * Jackson Null 值响应处理配置。
 * <p>
 * 用于统一接口 JSON 序列化中的 null 展示，降低前端空值判断成本。
 * 仅影响响应序列化，不改变 Java 对象本身的字段值，也不影响反序列化入参。
 * </p>
 * <p>
 * 转换规则：
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

    /**
     * 注册 null 值序列化规则。
     *
     * @return Jackson 构建器自定义器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonNullValueCustomizer() {
        return builder -> builder.postConfigurer(objectMapper -> {
            objectMapper.setSerializerFactory(
                    objectMapper.getSerializerFactory()
                            .withSerializerModifier(new NullValueBeanSerializerModifier()));
        });
    }

    /**
     * Bean 序列化属性修改器。
     * <p>
     * 根据字段声明类型替换 null 字段的写出逻辑。
     * </p>
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
         * 根据字段类型确定 null 值输出形态。
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
         * 判断字段是否属于数字类型。
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
         * 判断字段是否属于日期时间类型。
         * <p>
         * 日期时间 null 保持原样，避免前端把空时间误判为有效值。
         * </p>
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
     * Null 值输出类型。
     */
    public enum NullValueType {
        /**
         * null -> ""
         */
        STRING,
        /**
         * null -> []
         */
        ARRAY,
        /**
         * null -> {}
         */
        OBJECT,
        /**
         * null -> 0
         */
        NUMBER,
        /**
         * null -> false
         */
        BOOLEAN
    }

    /**
     * Null 字段属性写出器。
     * <p>
     * 仅在字段值为 null 时写出替代空值；非 null 值交回原始 writer 处理。
     * </p>
     */
    public static class NullValuePropertyWriter extends BeanPropertyWriter {

        private final BeanPropertyWriter delegate;
        private final NullValueType nullValueType;

        /**
         * 基于原始属性 writer 创建 null 值包装写出器。
         *
         * @param base          原始属性 writer
         * @param nullValueType null 值输出类型
         */
        public NullValuePropertyWriter(BeanPropertyWriter base, NullValueType nullValueType) {
            super(base);
            this.delegate = base;
            this.nullValueType = nullValueType;
        }

        /**
         * 写出单个字段，null 值按配置输出为空字符串、空数组、空对象、0 或 false。
         */
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

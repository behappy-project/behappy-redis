package org.xiaowu.behappy.redis.register;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.xiaowu.behappy.redis.annotation.KryoSerialize;

import java.io.Serializable;

/**
 *
 * @author xiaowu
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
//@KryoSerialize
public class User{

    //private static final long serialVersionUID = 1L;
    private String name;

    private Integer age;
}

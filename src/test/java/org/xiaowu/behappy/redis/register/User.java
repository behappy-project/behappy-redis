package org.xiaowu.behappy.redis.register;

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
@KryoSerialize
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;

    private Integer age;
}

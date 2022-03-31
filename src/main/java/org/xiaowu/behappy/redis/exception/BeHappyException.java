package org.xiaowu.behappy.redis.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 小五
 */
@AllArgsConstructor
public class BeHappyException extends RuntimeException{

    @Setter
    @Getter
    private int code;

    @Setter
    @Getter
    private String msg;

}
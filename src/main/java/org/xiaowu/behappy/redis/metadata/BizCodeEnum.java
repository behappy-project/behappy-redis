package org.xiaowu.behappy.redis.metadata;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BizCodeEnum {
	LOCKKEY_IS_NULL(10901, "LockKey 不可为空"),
	LOCK_WAIT_TIMEOUT(10902, "锁等待超时"),
	BEYOND_THE_FREQUENCY_LIMIT(10903, "超出频率限制"),
	REPEATED_REQUESTS(10904, "重复请求，请稍后重试");


	private int code;

	private String msg;

}
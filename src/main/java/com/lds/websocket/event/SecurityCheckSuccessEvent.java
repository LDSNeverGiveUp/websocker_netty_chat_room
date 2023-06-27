package com.lds.websocket.event;

import lombok.Data;

/**
 * @author lidongsheng
 */
@Data
public class SecurityCheckSuccessEvent {
    private String userId;
    private String appId;
    private String appKey;
}

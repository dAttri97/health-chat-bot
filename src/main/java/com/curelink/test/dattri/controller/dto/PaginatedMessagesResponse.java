package com.curelink.test.dattri.controller.dto;

import java.util.List;

/**
 * Paginated list of messages for "load more" / scroll-up history.
 */
public record PaginatedMessagesResponse(
    List<MessageResponse> messages,
    boolean hasMore,
    String nextBefore
) {}

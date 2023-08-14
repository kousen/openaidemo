package com.kousenit.picogen;

import java.util.List;

// Full Job response, which includes a List<GetResponse> as "items":
// [
//    null,
//    {
//        "items": [
//            {
//                "id": 5879034751746049,
//                "account_id": 5850868599488513,
//                "token_id": 5852032091029505,
//                "transaction_id": 5879034726580225,
//                "status": "completed",
//                "payload": {
//                    "model": "stability",
//                    "command": "generate",
//                    "options": {
//                        "size": "1344x768",
//                        "count": 1,
//                        "style": "photographic",
//                        "engine": "xl-v1.0",
//                        "prompt": "A realistic scene in cinematic style of man and woman talking in night club, neon color, portrait shot"
//                    },
//                    "version": 1
//                },
//                "result": [
//                    "http://api.picogen.io/files/202308/06/b66ef7b1656c904fe6ae982561a0f11f.png"
//                ],
//                "duration_ms": 21011,
//                "created_at": 1691342119,
//                "updated_at": 1691342140
//            }
//        ],
//        "total": 1,
//        "offset": 0,
//        "limit": 10,
//        "pagination": {
//            "has_pages": false,
//            "current": 1,
//            "last": 1,
//            "prev_page": null,
//            "next_page": null
//        }
//    }
//]

// status:  'created', 'processing', 'completed', or 'error'
public record GetResponse(long id, long accountId, long tokenId, long transactionId,
                          String status, Payload payload, List<String> result, int durationMs,
                          long createdAt, long updatedAt) {
    public record Payload(Options options, String model, String command, int version){
        public record Options(String size, String style,
                              String engine, String prompt) {}
    }
}

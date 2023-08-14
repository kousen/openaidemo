package com.kousenit.picogen;

// Example request:
// {
//    "version": 1,
//    "count": 1,  // not allowed for midjourney
//    "model": "stability",
//    "command": "generate",  // or "upscale"
//    "prompt": "A realistic scene in cinematic style of man and woman talking in night club, neon color, portrait shot",
//    "ratio": "16:9",
//    "style": "photographic", // used by "stability" model only
//    "engine": "xl-v1.0"     // not allowed for "dalle2"
// }
//
// also "size", but not allowed for midjourney
//      "ratio", cannot be set for stability and dalle2
//      "speed": "relax", "fast" (default), "turbo", only for midjourney

// Notes:
//  model:
//      'stability', 'midjourney', 'dalle2'
//  engine:
//      Stability: xl-v1.0 (default), 512-v2.1, 768-v2.1, v1.5
//      Midjourney: mj-4, mj-5.1, mj-5.1-raw, mj-5.2 (default), mj-5.2-raw, niji-4, niji-5,
//                  niji-5-cute, niji-5-expressive, niji-5-original, niji-5-scenic
public record GenerateRequest(int version, String model, String command,
                              String prompt, String ratio, String style, String engine) {}
package com.kousenit.stabilityai;

import java.util.List;

// From https://platform.stability.ai/docs/api-reference#tag/v1generation/operation/textToImage:

// for SDXL 1.0, valid dimensions are
//      1024x1024, 1152x896, 1216x832, 1344x768, 1536x640,
//      640x1536, 768x1344, 832x1216, 896x1152

// sampler: (Not included in record)
//      DDIM, DDPM, K_DPMPP_2M, K_DPMPP_2S_ANCESTRAL, K_DPM_2,
//      K_DPM_2_ANCESTRAL K_EULER K_EULER_ANCESTRAL K_HEUN K_LMS
//
//      Which sampler to use for the diffusion process. If this value is omitted
//      we'll automatically select an appropriate sampler for you.

// cfgScale: 0..35, Default: 7
//      How strictly the diffusion process adheres to the prompt text
//      (higher values keep your image closer to your prompt)

// clipGuidancePreset:
//      [FAST_BLUE, FAST_GREEN, NONE, SIMPLE, SLOW, SLOWER, SLOWEST], Default: NONE
//      Which sampler to use for the diffusion process. If this value is omitted
//      we'll automatically select an appropriate sampler for you.

// samples: 1..10, Default: 1
//      Number of images to generate

// seed: 0..4294967295, Default: 0
//      Random noise seed (omit this option or use 0 for a random seed)

// stylePreset: [3d-model, analog-film, anime, cinematic, comic-book, digital-art,
//      enhance,fantasy-art, isometric, line-art, low-poly, modeling-compound,
//      neon-punk, origami, photographic, pixel-art, tile-texture]

//      Pass in a style preset to guide the image model towards a particular style.
//      This list of style presets is subject to change.

public record Payload(int cfgScale, String clipGuidancePreset, String stylePreset,
                      int height, int width, int samples, int steps,
                      List<TextPrompt> textPrompts
) {}

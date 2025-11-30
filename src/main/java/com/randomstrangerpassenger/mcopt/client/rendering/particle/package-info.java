/**
 * Client-only particle utilities for MCOPT.
 *
 * <p>This package contains helper interfaces and classes that implement particle occlusion
 * culling in a lightweight, compatibility-friendly way. The system periodically tests whether
 * a particle is hidden behind opaque blocks from the player's camera and skips rendering when
 * appropriate. All state is stored on the particle instance itself to avoid global caches and
 * minimize synchronization overhead.</p>
 */
package com.randomstrangerpassenger.mcopt.client.rendering.particle;

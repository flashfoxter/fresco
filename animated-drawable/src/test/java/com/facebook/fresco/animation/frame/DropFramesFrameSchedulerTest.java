/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.facebook.fresco.animation.frame;

import javax.annotation.Nullable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;

import com.facebook.fresco.animation.backend.AnimationBackend;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests {@link DropFramesFrameScheduler}.
 */
public class DropFramesFrameSchedulerTest {

  private DummyAnimationBackend mDummyAnimationBackend;

  private DropFramesFrameScheduler mFrameScheduler;

  @Before
  public void setUp() throws Exception {
    mDummyAnimationBackend = new DummyAnimationBackend();
    mFrameScheduler = new DropFramesFrameScheduler(mDummyAnimationBackend);
  }

  @Test
  public void testGetFrameNumberToRender() throws Exception {
    assertThat(mFrameScheduler.getFrameNumberToRender(0, -1)).isEqualTo(0);
    assertThat(mFrameScheduler.getFrameNumberToRender(50, -1)).isEqualTo(0);
    assertThat(mFrameScheduler.getFrameNumberToRender(100, -1)).isEqualTo(1);
    assertThat(mFrameScheduler.getFrameNumberToRender(499, -1)).isEqualTo(4);
    assertThat(mFrameScheduler.getFrameNumberToRender(500, -1)).isEqualTo(0);
    assertThat(mFrameScheduler.getFrameNumberToRender(600, -1)).isEqualTo(1);
    assertThat(mFrameScheduler.getFrameNumberToRender(601, -1)).isEqualTo(1);
  }

  @Test
  public void testGetLoopDurationMs() throws Exception {
    assertThat(mFrameScheduler.getLoopDurationMs()).isEqualTo(500);
  }

  @Test
  public void testGetTargetRenderTimeMs() throws Exception {
    assertThat(mFrameScheduler.getTargetRenderTimeMs(0)).isEqualTo(0);
    assertThat(mFrameScheduler.getTargetRenderTimeMs(1)).isEqualTo(100);
    assertThat(mFrameScheduler.getTargetRenderTimeMs(2)).isEqualTo(200);
    assertThat(mFrameScheduler.getTargetRenderTimeMs(3)).isEqualTo(300);
    assertThat(mFrameScheduler.getTargetRenderTimeMs(4)).isEqualTo(400);
  }

  @Test
  public void testGetDelayUntilNextFrameMs() throws Exception {
    assertThat(mFrameScheduler.getDelayUntilNextFrameMs(0)).isEqualTo(0);
    assertThat(mFrameScheduler.getDelayUntilNextFrameMs(1)).isEqualTo(99);
    assertThat(mFrameScheduler.getDelayUntilNextFrameMs(50)).isEqualTo(50);
    assertThat(mFrameScheduler.getDelayUntilNextFrameMs(100)).isEqualTo(0);
    assertThat(mFrameScheduler.getDelayUntilNextFrameMs(170)).isEqualTo(30);
    assertThat(mFrameScheduler.getDelayUntilNextFrameMs(460)).isEqualTo(40);
    assertThat(mFrameScheduler.getDelayUntilNextFrameMs(510)).isEqualTo(90);
  }

  @Test
  public void testIsInfiniteAnimation() throws Exception {
    assertThat(mFrameScheduler.isInfiniteAnimation()).isFalse();
  }

  @Test
  public void testLoopCount() throws Exception {
    long animationDurationMs = mDummyAnimationBackend.getAnimationDurationMs();
    int lastFrameNumber = mDummyAnimationBackend.getFrameCount() - 1;

    assertThat(mFrameScheduler.getFrameNumberToRender(animationDurationMs, -1))
        .isEqualTo(FrameScheduler.FRAME_NUMBER_DONE);

    assertThat(mFrameScheduler.getFrameNumberToRender(animationDurationMs + 1, -1))
        .isEqualTo(FrameScheduler.FRAME_NUMBER_DONE);

    assertThat(mFrameScheduler.getFrameNumberToRender(
        animationDurationMs + mDummyAnimationBackend.getFrameDurationMs(lastFrameNumber), -1))
        .isEqualTo(FrameScheduler.FRAME_NUMBER_DONE);

    assertThat(mFrameScheduler.getFrameNumberToRender(
        animationDurationMs + mDummyAnimationBackend.getFrameDurationMs(lastFrameNumber) + 100, -1))
        .isEqualTo(FrameScheduler.FRAME_NUMBER_DONE);
  }

  @Test
  public void testGetFrameNumberWithinLoop() throws Exception {
    assertThat(mFrameScheduler.getFrameNumberWithinLoop(0)).isEqualTo(0);
    assertThat(mFrameScheduler.getFrameNumberWithinLoop(1)).isEqualTo(0);
    assertThat(mFrameScheduler.getFrameNumberWithinLoop(99)).isEqualTo(0);
    assertThat(mFrameScheduler.getFrameNumberWithinLoop(100)).isEqualTo(1);
    assertThat(mFrameScheduler.getFrameNumberWithinLoop(101)).isEqualTo(1);
    assertThat(mFrameScheduler.getFrameNumberWithinLoop(250)).isEqualTo(2);
    assertThat(mFrameScheduler.getFrameNumberWithinLoop(499)).isEqualTo(4);
  }

  private static class DummyAnimationBackend implements AnimationBackend {

    public long getLoopDurationMs() {
      long loopDuration = 0;
      for (int i = 0; i < getFrameCount(); i++) {
        loopDuration += getFrameDurationMs(i);
      }
      return loopDuration;
    }

    public long getAnimationDurationMs() {
      return getLoopDurationMs() * getLoopCount();
    }

    @Override
    public int getFrameCount() {
      return 5;
    }

    @Override
    public int getFrameDurationMs(int frameNumber) {
      return 100;
    }

    @Override
    public int getLoopCount() {
      return 7;
    }

    @Override
    public boolean drawFrame(
        Drawable parent, Canvas canvas, int frameNumber) {
      return false;
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public void setBounds(Rect bounds) {

    }

    @Override
    public int getSizeInBytes() {
      return 0;
    }
  }
}
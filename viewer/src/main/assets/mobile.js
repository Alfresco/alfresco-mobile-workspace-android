/* Copyright 2020 Alfresco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Support library for adding gestures functionality for mobile
 */
function enableGestureSupport() {
  const MIN_TOUCH_DELAY = 300;
  const MAX_TOUCH_DIST = 44;

  let startX = 0;
  let startY = 0;
  let originX = 0;
  let originY = 0;
  let initialPinchDistance = 0;
  let pinchScale = 1;
  let lastTouchTime = 0;
  let lastTouch = null;
  let lastScrollTime = 0;

  const viewer = document.getElementById('viewer');
  const container = document.getElementById('viewerContainer');

  const reset = () => { startX = startY = initialPinchDistance = 0; pinchScale = 1; };
  const clamp = (val, min, max) => Math.min(Math.max(val, min), max);
  const floatEqual = (n1, n2, precision) => Math.abs(n1 - n2) <= precision;
  const dist = (t1, t2) => Math.hypot((t2.pageX - t1.pageX), (t2.pageY - t1.pageY));

  const pdfApp = PDFViewerApplication;
  const { pdfViewer } = pdfApp;

  document.addEventListener('touchstart', (e) => {
    if (e.touches.length === 1) {
      const touch = e.touches[0];
      const distToLast = lastTouch != null ? dist(touch, lastTouch) : 0;

      // Is double tap, if delay and touch distance is short
      // and not currently scrolling.
      if (
        e.timeStamp - lastTouchTime < MIN_TOUCH_DELAY
        && e.timeStamp - lastScrollTime > MIN_TOUCH_DELAY
        && distToLast < MAX_TOUCH_DIST
      ) {
        lastTouchTime = 0;

        const px = (touch.pageX + container.scrollLeft) / container.scrollWidth;
        const py = (touch.pageY + container.scrollTop) / container.scrollHeight;
        if (pdfViewer.currentScaleValue === 'auto' || floatEqual(pdfViewer.currentScale, pdfApp.initialScale, 0.0001)) {
          pdfViewer.currentScaleValue = 'page-actual';

          container.scrollLeft = container.scrollWidth * px - container.clientWidth / 2;
          container.scrollTop = container.scrollHeight * py - container.clientHeight / 2;
        } else {
          pdfViewer.currentScaleValue = 'auto';
        }

        reset();
      } else {
        lastTouch = e.touches[0];
        lastTouchTime = e.timeStamp;
      }
    }

    if (e.touches.length > 1) {
      startX = (e.touches[0].pageX + e.touches[1].pageX) / 2;
      startY = (e.touches[0].pageY + e.touches[1].pageY) / 2;
      initialPinchDistance = dist(e.touches[0], e.touches[1]);
    } else {
      initialPinchDistance = 0;
    }
  });
  document.addEventListener('touchmove', (e) => {
    if (initialPinchDistance <= 0 || e.touches.length < 2) { return; }
    if (e.scale !== 1 && e.cancelable) { e.preventDefault(); }

    // Update start point to enable panning while zooming
    startX = (e.touches[0].pageX + e.touches[1].pageX) / 2;
    startY = (e.touches[0].pageY + e.touches[1].pageY) / 2;

    const pinchDistance = dist(e.touches[0], e.touches[1]);
    originX = startX + container.scrollLeft;
    originY = startY + container.scrollTop;
    pinchScale = pinchDistance / initialPinchDistance;
    viewer.style.transform = `scale(${pinchScale})`;
    viewer.style.transformOrigin = `${originX}px ${originY}px`;
  }, { passive: false });
  document.addEventListener('touchend', (e) => {
    if (initialPinchDistance <= 0) { return; }
    viewer.style.transform = 'none';
    viewer.style.transformOrigin = 'unset';

    // Compute the current center point in page coordinates
    const pageCenterX = container.clientWidth / 2 + container.scrollLeft;
    const pageCenterY = container.clientHeight / 2 + container.scrollTop;

    // Compute the next center point in page coordinates
    const centerX = (pageCenterX - originX) / pinchScale + originX;
    const centerY = (pageCenterY - originY) / pinchScale + originY;

    // Compute the ratios of the center point to the total scrollWidth/scrollHeight
    const px = centerX / container.scrollWidth;
    const py = centerY / container.scrollHeight;

    // Scale
    const newScale = pdfViewer.currentScale * pinchScale;
    pdfViewer.currentScale = clamp(newScale, pdfApp.initialScale, pdfApp.maxScale);

    // Set the scrollbar positions using the percentages and the new scrollWidth/scrollHeight
    container.scrollLeft = container.scrollWidth * px - container.clientWidth / 2;
    container.scrollTop = container.scrollHeight * py - container.clientHeight / 2;
    reset();
  });
  document.querySelector('#viewerContainer').addEventListener('scroll', (e) => {
    lastScrollTime = e.timeStamp;
  });
}

document.addEventListener(
  'DOMContentLoaded',
  () => {
    enableGestureSupport();
  },
  true,
);

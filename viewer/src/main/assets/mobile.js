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
let pinchZoomEnabled = false;
function enablePinchZoom() {
    let startX = 0, startY = 0, originX = 0, originY = 0;
    let initialPinchDistance = 0;
    let pinchScale = 1;
    let lastTouchTime = 0;
    const viewer = document.getElementById("viewer");
    const container = document.getElementById("viewerContainer");
    const reset = () => { startX = startY = initialPinchDistance = 0; pinchScale = 1; };
    // Prevent native iOS page zoom
    //document.addEventListener("touchmove", (e) => { if (e.scale !== 1) { e.preventDefault(); } }, { passive: false });
    document.addEventListener("touchstart", (e) => {
        if (e.touches.length == 1) {
            if (e.timeStamp - lastTouchTime < 500) {
                lastTouchTime = 0;

                const pdfViewer = PDFViewerApplication.pdfViewer;
                const px = (e.touches[0].pageX + container.scrollLeft) / container.scrollWidth
                const py = (e.touches[0].pageY + container.scrollTop) / container.scrollHeight
                if (pdfViewer.currentScaleValue === "auto") {
                    pdfViewer.currentScaleValue = "page-actual";

                    container.scrollLeft = container.scrollWidth * px - container.clientWidth / 2
                    container.scrollTop = container.scrollHeight * py - container.clientHeight / 2
                } else {
                    pdfViewer.currentScaleValue = "auto";
                }

                reset();
            } else {
                lastTouchTime = e.timeStamp;
            }
        }

        if (e.touches.length > 1) {
            startX = (e.touches[0].pageX + e.touches[1].pageX) / 2;
            startY = (e.touches[0].pageY + e.touches[1].pageY) / 2;
            initialPinchDistance = Math.hypot((e.touches[1].pageX - e.touches[0].pageX), (e.touches[1].pageY - e.touches[0].pageY));
        } else {
            initialPinchDistance = 0;
        }
    });
    document.addEventListener("touchmove", (e) => {
        if (initialPinchDistance <= 0 || e.touches.length < 2) { return; }
        if (e.scale !== 1) { e.preventDefault(); }

        // Update start point to enable panning while zooming
        startX = (e.touches[0].pageX + e.touches[1].pageX) / 2;
        startY = (e.touches[0].pageY + e.touches[1].pageY) / 2;

        const pinchDistance = Math.hypot((e.touches[1].pageX - e.touches[0].pageX), (e.touches[1].pageY - e.touches[0].pageY));
        originX = startX + container.scrollLeft;
        originY = startY + container.scrollTop;
        pinchScale = pinchDistance / initialPinchDistance;
        viewer.style.transform = `scale(${pinchScale})`;
        viewer.style.transformOrigin = `${originX}px ${originY}px`;
    }, { passive: false });
    document.addEventListener("touchend", (e) => {
        if (initialPinchDistance <= 0) { return; }
        viewer.style.transform = `none`;
        viewer.style.transformOrigin = `unset`;

        // Compute the current center point in page coordinates
        const pageCenterX = container.clientWidth/2 + container.scrollLeft;
        const pageCenterY = container.clientHeight/2 + container.scrollTop;

        // Compute the next center point in page coordinates
        const centerX = (pageCenterX - originX) / pinchScale + originX;
        const centerY = (pageCenterY - originY) / pinchScale + originY;

        // Compute the ratios of the center point to the total scrollWidth/scrollHeight
        const px = centerX / container.scrollWidth;
        const py = centerY / container.scrollHeight;

        // Scale
        PDFViewerApplication.pdfViewer.currentScale *= pinchScale;

        // Set the scrollbar positions using the percentages and the new scrollWidth/scrollHeight
        container.scrollLeft = container.scrollWidth * px - container.clientWidth/2;
        container.scrollTop = container.scrollHeight * py - container.clientHeight/2;
        reset();
    });
}

document.addEventListener(
  "DOMContentLoaded",
  function () {
    if (!pinchZoomEnabled) {
        pinchZoomEnabled = true;
        enablePinchZoom();
    }
  },
  true
);

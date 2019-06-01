package services.callback.markup.banner

import com.appodealx.exchange.common.models.CallbackMacros.{PlacementId, SegmentId}
import com.appodealx.exchange.common.models.Uri
import com.appodealx.exchange.common.utils.StringWithMacroDecorator

object BannerMarkups {

  def jsScriptForBanner(impressionUrl: Uri, clickUrl: Uri, finishUrl: Uri) =
    s"""
       |<script type="application/javascript">
       |    var appodealXSegmentId, appodealXPlacementId;
       |    var appodealXImpressionTrackers = ["${impressionUrl.toString}"];
       |    var appodealXClickTrackers = ["${clickUrl.toString}"];
       |    var appodealXFinishTrackers = ["${finishUrl.toString}"];
       |    var appodealXImpressionTracked = false;
       |    var appodealXClickTracked = false;
       |    var appodealXFinishTracked = false;
       |
       |    var appodealXSendImpression = function () {
       |        if (!appodealXImpressionTracked) {
       |            var hiddenSpan = document.createElement('span');
       |            hiddenSpan.style.display = 'none';
       |            appodealXImpressionTrackers.forEach(function (tracker) {
       |                var img = document.createElement('img');
       |                if (typeof appodealXSegmentId == 'string' && tracker.indexOf('${SegmentId.asMacro(true)}') > 0) {
       |                    tracker = tracker.replace("${SegmentId.asMacro(true)}", appodealXSegmentId)
       |                }
       |                if (typeof appodealXPlacementId == 'string' && tracker.indexOf('${PlacementId.asMacro(true)}') > 0) {
       |                    tracker = tracker.replace("${PlacementId.asMacro(true)}", appodealXPlacementId)
       |                }
       |                img.src = tracker;
       |                hiddenSpan.appendChild(img);
       |                document.body.appendChild(hiddenSpan);
       |            });
       |            appodealXImpressionTracked = true;
       |        }
       |    };
       |
       |    var appodealXSendClicks = function () {
       |        if (!appodealXImpressionTracked) {
       |            appodealXSendImpression();
       |        }
       |        if (!appodealXClickTracked) {
       |            var hiddenSpan = document.createElement('span');
       |            hiddenSpan.style.display = 'none';
       |            appodealXClickTrackers.forEach(function (tracker) {
       |                var img = document.createElement('img');
       |                if (typeof appodealXSegmentId == 'string' && tracker.indexOf('${SegmentId.asMacro(true)}') > 0) {
       |                    tracker = tracker.replace("${SegmentId.asMacro(true)}", appodealXSegmentId)
       |                }
       |                if (typeof appodealXPlacementId == 'string' && tracker.indexOf('${PlacementId.asMacro(true)}') > 0) {
       |                    tracker = tracker.replace("${PlacementId.asMacro(true)}", appodealXPlacementId)
       |                }
       |                img.src = tracker;
       |                hiddenSpan.appendChild(img);
       |                document.body.appendChild(hiddenSpan);
       |            });
       |            appodealXClickTracked = true;
       |        }
       |    };
       |
       |    var appodealXTrackViewableChange = function (viewable) {
       |        mraid.addEventListener('viewableChange', function() { console.log('empty viewableChange event'); } );
       |        if (viewable) {
       |            appodealXSendImpression();
       |        }
       |    };
       |
       |    var appodealXSubscribeViewableEvent = function () {
       |        mraid.addEventListener('ready', function() { console.log('empty ready event'); });
       |        if (mraid.isViewable()) {
       |            appodealXSendImpression();
       |        }
       |        else {
       |            mraid.addEventListener('viewableChange', function() {} );
       |            mraid.addEventListener('viewableChange', appodealXTrackViewableChange);
       |        }
       |    };
       |
       |    if (mraid.getState() === 'loading') {
       |        mraid.addEventListener('ready', function(){} );
       |        mraid.addEventListener('ready', appodealXSubscribeViewableEvent);
       |    } else {
       |        appodealXSubscribeViewableEvent();
       |    }
       |
       |    var appodealXSetSegmentAndPlacement = function (segmentId, placementId) {
       |        appodealXSegmentId = segmentId;
       |        appodealXPlacementId = placementId;
       |        console.log('Set segment: ' + appodealXSegmentId);
       |        console.log('Set placement: ' + appodealXPlacementId);
       |    };
       |
       |    var appodealXTrackFinishEvent = function() {
       |        if (!appodealXFinishTracked) {
       |            var hiddenSpan = document.createElement('span');
       |            hiddenSpan.style.display = 'none';
       |            appodealXFinishTrackers.forEach(function (tracker) {
       |                var img = document.createElement('img');
       |                if (typeof appodealXSegmentId == 'string' && tracker.indexOf('${SegmentId.asMacro(true)}') > 0) {
       |                    tracker = tracker.replace("${SegmentId.asMacro(true)}", appodealXSegmentId)
       |                }
       |                if (typeof appodealXPlacementId == 'string' && tracker.indexOf('${PlacementId.asMacro(true)}') > 0) {
       |                    tracker = tracker.replace("${PlacementId.asMacro(true)}", appodealXPlacementId)
       |                }
       |                img.src = tracker;
       |                hiddenSpan.appendChild(img);
       |                document.body.appendChild(hiddenSpan);
       |            });
       |            appodealXFinishTracked = true;
       |        }
       |    }
       |</script>""".stripMargin

  def pixelTracker(url: Uri) = {
    s"""
       |<img src="${url.toString}" style="display: none;">
    """.stripMargin
  }
}
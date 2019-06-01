package services.callback.injectors

import services.callback.injectors.banner.LegacyBannerCallbackInjectorImpl
import services.callback.injectors.nast.LegacyNativeCallbackInjectorImpl
import services.callback.injectors.vast.LegacyVideoCallbackInjectorImpl


class LegacyCallbackInjectorImpl(bannerInst: LegacyBannerCallbackInjectorImpl,
                                 nativeInst: LegacyNativeCallbackInjectorImpl,
                                 videoInst: LegacyVideoCallbackInjectorImpl)
  extends CallbackInjector(bannerInst, nativeInst, videoInst)

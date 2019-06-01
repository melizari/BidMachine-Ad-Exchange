package services.callback.injectors

import services.callback.injectors.banner.BannerCallbackInjectorImpl
import services.callback.injectors.nast.NativeCallbackInjectorImpl
import services.callback.injectors.vast.VideoCallbackInjectorImpl


class CallbackInjectorImpl(bannerInst: BannerCallbackInjectorImpl,
                           nativeInst: NativeCallbackInjectorImpl,
                           videoInst: VideoCallbackInjectorImpl)
  extends CallbackInjector(bannerInst, nativeInst, videoInst)
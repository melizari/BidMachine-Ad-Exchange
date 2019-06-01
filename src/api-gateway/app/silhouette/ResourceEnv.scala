package silhouette

import com.mohiva.play.silhouette.api.Env

trait ResourceEnv extends Env {
  type R
  type I = Account[R]
}

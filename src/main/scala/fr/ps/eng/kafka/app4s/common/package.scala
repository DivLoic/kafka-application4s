package fr.ps.eng.kafka.app4s

import java.security.MessageDigest

import sun.misc.BASE64Encoder

/**
 * Created by loicmdivad.
 */
package object common {

  val Base64Encoder: BASE64Encoder = new sun.misc.BASE64Encoder()
  val SHA1Digest: MessageDigest = java.security.MessageDigest.getInstance("SHA-1")

  def sha1(name: String): String = Base64Encoder.encode(SHA1Digest.digest(name.getBytes))
}

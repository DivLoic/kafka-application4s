package fr.ps.eng.kafka.app4s

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64

/**
 * Created by loicmdivad.
 */
package object common {

  val Base64Encoder: Base64.Encoder = java.util.Base64.getEncoder
  val SHA1Digest: MessageDigest = java.security.MessageDigest.getInstance("SHA-1")

  def sha1(name: String): String =
    new String(Base64Encoder.encode(SHA1Digest.digest(name.getBytes)), StandardCharsets.UTF_8)
}

/* groovylint-disable all */
package nfneuro.plugin.config

import spock.lang.Specification
import java.nio.file.Files

class BidsConfigLoaderSpec extends Specification {

    def 'load should reject configs using reserved meta key'() {
        given:
        def tmpFile = Files.createTempFile('bidsconfig', '.yml').toFile()
        tmpFile.write("""
meta:
  dummy: true
loop_over: ['subject']
""")
        def loader = new BidsConfigLoader()

        when:
        loader.load(tmpFile.absolutePath)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("Reserved key 'meta' cannot be used as suffix name")
    }

}

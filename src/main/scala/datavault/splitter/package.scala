package jsonsplitter

import zio._

package object splitter {
  type Splitter = Has[Splitter.Service]
}

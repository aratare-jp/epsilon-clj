(ns altio.core
  (:gen-class)
  (:require [cprop.core :as cp])
  (:import (com.altio ArrayInitListener
                      ArrayInitParser$InitContext
                      ArrayInitParser$ValueContext ArrayInitLexer ArrayInitParser)
           (org.antlr.v4.runtime CharStreams CommonTokenStream ParserRuleContext)
           (org.antlr.v4.runtime.tree ParseTreeWalker ErrorNode TerminalNode ParseTreeListener)))

(defrecord Walker []
  ArrayInitListener
  (^void visitTerminal [^ParseTreeListener this
                        ^TerminalNode ctx])

  (^void visitErrorNode [^ParseTreeListener this
                         ^ErrorNode ctx])

  (^void enterEveryRule [^ParseTreeListener this
                         ^ParserRuleContext ctx])

  (^void exitEveryRule [^ParseTreeListener this
                        ^ParserRuleContext ctx])

  (^void enterInit [^ArrayInitListener this
                    ^ArrayInitParser$InitContext ctx]
    (print "\""))

  (^void exitInit [^ArrayInitListener this
                   ^ArrayInitParser$InitContext ctx]
    (println "\""))

  (^void enterValue [^ArrayInitListener this
                     ^ArrayInitParser$ValueContext ctx]
    (let [^String value (-> ctx .INT .getText)]
      (printf "\\u%04x" (Integer/valueOf value))))

  (^void exitValue [^ArrayInitListener this
                    ^ArrayInitParser$ValueContext ctx]))

(defn -main [& args]
  (print "Enter array: ")
  (let [input  (CharStreams/fromStream System/in)
        lexer  (new ArrayInitLexer input)
        tokens (new CommonTokenStream lexer)
        parser (new ArrayInitParser tokens)
        tree   (.init parser)
        walker (new ParseTreeWalker)]
    (.walk walker (new Walker) tree)))
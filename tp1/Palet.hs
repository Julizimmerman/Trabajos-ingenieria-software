module Palet ( Palet, newP, destinationP, netP )
  where

data Palet = Pal String Int deriving (Eq, Show)

newP :: String -> Int -> Palet   -- construye un Palet dada una ciudad de destino y un peso en toneladas
destinationP :: Palet -> String  -- responde la ciudad destino del palet
netP :: Palet -> Int             -- responde el peso en toneladas del palet
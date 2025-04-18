module Palet ( Palet, newP, destinationP, netP )
  where

data Palet = Pal String Int deriving (Eq, Show)

newP :: String -> Int -> Palet   -- construye un Palet dada una ciudad de destino y un peso en toneladas
newP city weight | city == "" = error "La ciudad de destino no puede estar vacía"
                 | weight <= 0 = error "El peso del palet debe ser mayor a 0"
                 | otherwise = Pal city weight

destinationP :: Palet -> String  -- responde la ciudad destino del palet
destinationP (Pal city _) = city -- Si no ponemos los paréntesis, el compilador piensa que Pal es una función y da error. 

netP :: Palet -> Int             -- responde el peso en toneladas del palet
netP (Pal _ weight) = weight     -- Si no ponemos los paréntesis, el compilador piensa que Pal es una función y da error.


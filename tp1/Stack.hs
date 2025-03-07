module Stack ( Stack, newS, freeCellsS, stackS, netS, holdsS, popS )
  where

import Palet
import Route

data Stack = Sta [ Palet ] Int deriving (Eq, Show)

newS :: Int -> Stack                      -- construye una Pila con la capacidad indicada 
freeCellsS :: Stack -> Int                -- responde la celdas disponibles en la pila
stackS :: Stack -> Palet -> Stack         -- apila el palet indicado en la pila
netS :: Stack -> Int                      -- responde el peso neto de los paletes en la pila
holdsS :: Stack -> Palet -> Route -> Bool -- indica si la pila puede aceptar el palet considerando las ciudades en la ruta
popS :: Stack -> String -> Stack          -- quita del tope los paletes con destino en la ciudad indicada

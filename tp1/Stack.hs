module Stack ( Stack, newS, freeCellsS, stackS, netS, holdsS, popS )
  where

import Palet
import Route

data Stack = Sta [ Palet ] Int deriving (Eq, Show)

newS :: Int -> Stack                      -- construye una Pila con la capacidad indicada 
newS capacity = Sta [] capacity           -- Inicialmente la pila está vacía 


freeCellsS :: Stack -> Int                                      -- responde la celdas disponibles en la pila
freeCellsS (Sta pallets capacity) = capacity - length pallets   -- Resta la cantidad de palets a la capacidad total


stackS :: Stack -> Palet -> Stack         -- apila el palet indicado en la pila
-- agregar el palet a la lista del stack y restarle 1 a capacity 
stackS (Sta pallets capacity) palet | (capacity - length pallets) > 0 = Sta (palet : pallets) capacity  -- Agrega el palet 
                                    | otherwise    = error "La pila está llena, no se puede apilar más pallets"


netS :: Stack -> Int                           -- responde el peso neto de los paletes en la pila
netS (Sta pallets _) = sum [netP p | p <- pallets]

holdsS :: Stack -> Palet -> Route -> Bool -- indica si la pila puede aceptar el palet considerando las ciudades en la ruta
holdsS (Sta [] _) palet _ = True  -- Si la pila está vacía, se puede agregar cualquier palet.
holdsS (Sta (headPalet : _) _) palet route = inOrderR route (destinationP palet) (destinationP headPalet)  -- Compara las ciudades de los palets

popS :: Stack -> String -> Stack          -- quita del tope los paletes con destino en la ciudad indicada
popS (Sta [] capacity) city = Sta [] capacity 
popS (Sta (headP : pallets) capacity) city | destinationP headP == city = popS (Sta pallets capacity) city 
                                           | otherwise = Sta (headP : pallets) capacity 


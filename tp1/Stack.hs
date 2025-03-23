module Stack ( Stack, newS, freeCellsS, stackS, netS, holdsS, popS )
  where

import Palet
import Route

data Stack = Sta [ Palet ] Int deriving (Eq, Show)

newS :: Int -> Stack                      -- construye una Pila con la capacidad indicada 
newS capacity = Sta [] capacity           -- Inicialmente la pila está vacía 

freeCellsS :: Stack -> Int                                      -- responde la celdas disponibles en la pila
freeCellsS (Sta pallets capacity) = capacity - length pallets   -- Resta la cantidad de palets a la capacidad total


stackS :: Stack -> Palet -> Stack  -- apila el palet indicado en la pila 
                                   -- ACLARACION: no chequea rutas (eso lo hace en etapas previas el camion)

stackS (Sta pallets capacity) palet
    | freeCellsS (Sta pallets capacity) > 0 
      && (netS (Sta pallets capacity) + netP palet) <= 10
    = Sta (palet : pallets) capacity  -- Agregamos el palet si pasa las condiciones
    | otherwise 
    = error "No se puede apilar el palet"

holdsS :: Stack -> Palet -> Route -> Bool -- indica si la pila puede aceptar el palet considerando las ciudades en la ruta
holdsS stack@(Sta [] _) _ _ = True  -- Si la pila está vacía, podemos apilar cualquier palet.
holdsS stack@(Sta (headPalet : _) _) palet route =
  inOrderR route (destinationP palet) (destinationP headPalet)  -- Verifica que el palet sigue el orden de la ruta
  && freeCellsS stack > 0  -- Verifica que haya espacio en la pila
  && (netS stack + netP palet) <= 10  -- Verifica que el peso no exceda 10 toneladas


popS :: Stack -> String -> Stack
popS (Sta pallets capacity) city = Sta (filter (\p -> destinationP p /= city) pallets) capacity


netS :: Stack -> Int                           -- responde el peso neto de los paletes en la pila
netS (Sta pallets _) = sum [netP p | p <- pallets]

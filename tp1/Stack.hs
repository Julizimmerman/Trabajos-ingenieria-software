module Stack ( Stack, newS, freeCellsS, stackS, netS, holdsS, popS )
  where

import Palet
import Route

data Stack = Sta [ Palet ] Int deriving (Eq, Show)

newS :: Int -> Stack                      -- construye una Pila con la capacidad indicada 
newS capacity = Sta [] capacity           -- Inicialmente la pila está vacía 


freeCellsS :: Stack -> Int                -- responde la celdas disponibles en la pila
freeCellsS (Sta _ capacity) = capacity    -- Devuelve la capacidad de la pila


stackS :: Stack -> Palet -> Stack         -- apila el palet indicado en la pila
-- agregar el palet a la lista del stack y restarle 1 a capacity 
stackS (Sta pallets capacity) palet | capacity > 0 = Sta (palet : pallets) (capacity - 1)  -- Agrega el palet y reduce la capacidad
                                    | otherwise    = error "La pila está llena, no se puede apilar más pallets"


netS :: Stack -> Int                      -- responde el peso neto de los paletes en la pila
netS (Sta pallets _) = sum (map netP pallets)  -- Suma los pesos de los palets

holdsS :: Stack -> Palet -> Route -> Bool -- indica si la pila puede aceptar el palet considerando las ciudades en la ruta
-- necesito la ciudad del palet head y a esa compararla con la ciudad del palet que quiero agregar usando la funcion inOrderR
-- si la ciudad del palet que quiero agregar viene antes que la ciudad del palet head, entonces puedo agregarlo
holdsS (Sta pallets capacity) palet (Rou cities) = case pallets of
    [] -> True
    (headPallet : _) -> inOrderR (Rou cities) (destinationP palet) (destinationP headPallet) && capacity > 0


popS :: Stack -> String -> Stack          -- quita del tope los paletes con destino en la ciudad indicada

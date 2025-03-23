module Truck ( Truck, newT, freeCellsT, loadT, unloadT, netT )
  where

import Palet
import Stack
import Route

data Truck = Tru [ Stack ] Route deriving (Eq, Show)

newT :: Int -> Int -> Route -> Truck  -- construye un camion según una cantidad de bahias, la altura de las mismas y una ruta
newT nStack capacity route = Tru [newS capacity | l <- [1..nStack]] route     

freeCellsT :: Truck -> Int            -- responde la celdas disponibles en el camion
-- freeCellsT (Tru stacks _) = sum [freeCellsS s | s <- stacks]  
freeCellsT (Tru stacks _) = foldr (\s fold -> freeCellsS s + fold) 0 stacks

loadT :: Truck -> Palet -> Truck      -- carga un palet en el camion
-- loadT (Tru stacks route) palet 
--                                 | not (inRouteR route (destinationP palet)) = error "Este destino no se encuentra dentro de la ruta de este Truck" 
--                                 | netT (Tru stacks route) == (10 * length stacks) = error "No se puede agregar más palets a este camion porque llegó a su peso limite" 
--                                 | freeCellsT (Tru stacks route) == 0 = error "No se pueden agregar más palets a este camion porque no le quedan espacio a sus Stacks "
                                
-- loadT (Tru (headS : stacks) route) palet                                 
--                                 | netS headS == 10 = loadT (Tru stacks route) palet
--                                 | holdsS headS palet route && freeCellsS headS > 0 && netS headS + netP palet < 10 = Tru (stackS headS palet:stacks) route 
--                                 | null stacks = error "No hay espacio en ningún stack para almacenar el palet"
--                                 | otherwise = loadT (Tru stacks route) palet  
loadT (Tru stacks route) palet
    | not (inRouteR route (destinationP palet)) = error "Este destino no se encuentra dentro de la ruta de este Truck"
    | netT (Tru stacks route) == (10 * length stacks) = error "No se puede agregar más palets a este camion porque llegó a su peso límite"
    | freeCellsT (Tru stacks route) == 0 = error "No se pueden agregar más palets a este camion porque no le quedan espacios en sus Stacks"
    | otherwise = Tru (tryLoad stacks []) route
  where
    tryLoad :: [Stack] -> [Stack] -> [Stack] -- intenta cargar un palet en alguno de los stacks y devuelve la lista de stacks actualizada
    tryLoad [] _ = error "No hay espacio en ningún stack para almacenar el palet"
    tryLoad (s:ss) anteriores
        | holdsS s palet route && freeCellsS s > 0 && netS s + netP palet <= 10 =
            reverse anteriores ++ (stackS s palet) : ss
        | otherwise = tryLoad ss (s:anteriores)



unloadT :: Truck -> String -> Truck   -- responde un camion al que se le han descargado los paletes que podían descargarse en la ciudad
unloadT (Tru stacks route) city = Tru [popS s city | s <- stacks] route 

netT :: Truck -> Int                  -- responde el peso neto en toneladas de los paletes en el camion
netT (Tru stacks _) = foldr (\s fold -> netS s + fold) 0 stacks

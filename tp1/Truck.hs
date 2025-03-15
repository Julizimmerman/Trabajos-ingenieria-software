module Truck ( Truck, newT, freeCellsT, loadT, unloadT, netT )
  where

import Palet
import Stack
import Route

data Truck = Tru [ Stack ] Route deriving (Eq, Show)

newT :: Int -> Int -> Route -> Truck  -- construye un camion según una cantidad de bahias, la altura de las mismas y una ruta
newT nStack capacity route = Tru [newS capacity | l <- [1..nStack]] newR route   

freeCellsT :: Truck -> Int            -- responde la celdas disponibles en el camion
-- freeCellsT (Tru stacks _) = sum [freeCellsS s | s <- stacks]  
freeCellsT (Tru stacks _) = foldr (\s fold -> freeCellsS s + fold) 0 stacks

loadT :: Truck -> Palet -> Truck      -- carga un palet en el camion

unloadT :: Truck -> String -> Truck   -- responde un camion al que se le han descargado los paletes que podían descargarse en la ciudad
unloadT (Tru stacks route) city = Tru [popS s city | s <- stacks] route 

netT :: Truck -> Int                  -- responde el peso neto en toneladas de los paletes en el camion
netT (Tru stacks _) = foldr (\s fold -> netS s + fold) 0 stacks

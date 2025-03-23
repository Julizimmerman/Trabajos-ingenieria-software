
module Route ( Route, newR, inOrderR, inRouteR )
  where

import Data.List (elemIndex)
data Route = Rou [ String ] deriving (Eq, Show)

newR :: [ String ] -> Route                    -- construye una ruta segun una lista de ciudades
-- me fijo que no me pasen una lista vacía y devuelvo la ruta con las ciudades en el orden que me pasaron
newR [] = error "La lista de ciudades no puede ser vacía"
newR cities = Rou cities 


inOrderR :: Route -> String -> String -> Bool
inOrderR (Rou cities) "" _ = error "La primera ciudad no puede ser vacía"
inOrderR (Rou cities) _ "" = error "La segunda ciudad no puede ser vacía"
inOrderR (Rou cities) city1 city2 = 
  case (elemIndex city1 cities, elemIndex city2 cities) of
    (Just ix, Just iy) -> ix < iy  -- True si city1 está antes que city2 en la lista
    (Nothing, _) -> error $ "La ciudad " ++ city1 ++ " no está en la ruta"
    (_, Nothing) -> error $ "La ciudad " ++ city2 ++ " no está en la ruta"

inRouteR :: Route -> String -> Bool -- indica si la ciudad consultada está en la ruta
inRouteR (Rou cities) city = city `elem` cities

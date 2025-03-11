
module Route ( Route, newR, inOrderR )
  where

import Data.List (elemIndex)
data Route = Rou [ String ] deriving (Eq, Show)

newR :: [ String ] -> Route                    -- construye una ruta segun una lista de ciudades
newR cities = Rou cities 

inOrderR :: Route -> String -> String -> Bool  -- indica si la primer ciudad consultada esta antes que la segunda ciudad en la ruta
-- asumo que la lista dada en newR tiene a las ciudades en orden y me fijo cual viene primera de las dos que me pasan en esta funcion 

inOrderR (Rou cities) city1 city2 = case (elemIndex city1 cities, elemIndex city2 cities) of
    (Just ix, Just iy) -> ix < iy   -- True if city1 comes before city2
    _ -> False                      -- False if either city is missing


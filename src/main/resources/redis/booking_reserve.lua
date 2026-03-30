local seatsKey = KEYS[1]
local membersKey = KEYS[2]
local memberId = ARGV[1]

local seats = redis.call('GET', seatsKey)
if (not seats) then
  return -3
end

if redis.call('SISMEMBER', membersKey, memberId) == 1 then
  return -2
end

local n = tonumber(seats)
if n <= 0 then
  return -1
end

redis.call('DECR', seatsKey)
redis.call('SADD', membersKey, memberId)
return n - 1

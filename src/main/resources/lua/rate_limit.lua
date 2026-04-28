local key = KEYS[1]
local maxAttempts = tonumber(ARGV[1])
local cooldown = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local data = redis.call("GET", key)

if not data then
    local value = cjson.encode({count=1, last=now})
    redis.call("SET", key, value, "EX", cooldown)
    return {1, 0}  -- allowed
end

local obj = cjson.decode(data)

local diff = now - obj.last

if diff < cooldown then
    return {0, cooldown - diff} -- reject
end

if obj.count >= maxAttempts then
    return {-1, 0} -- max reached
end

obj.count = obj.count + 1
obj.last = now

redis.call("SET", key, cjson.encode(obj), "EX", cooldown)

return {1, 0}
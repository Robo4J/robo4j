function parseJSON(json) { return convert(JSON.parse(json)); }

function convert(object) {
    if (isObject(object)) return toHashMap(object);
    if (isArray(object)) return toArray(object);
    return object;
}

function toHashMap(object) {
    var map = new java.util.HashMap();
    for (key in object) map.put(key, convert(object[key]));
    return map;
}

function toArray(object) {
    var array = Java.to(object, "java.lang.Object[]");
    for (var index = 0, len = array.length; index < len; index++) array[index] = convert(array[index]);
    return array;
}

function isObject(object) { return Object.prototype.toString.call(object) === "[object Object]"; }
function isArray(object) { return Object.prototype.toString.call(object) === "[object Array]"; }